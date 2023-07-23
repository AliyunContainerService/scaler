/*
Copyright 2023 The Alibaba Cloud Serverless Authors.
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at
    http://www.apache.org/licenses/LICENSE-2.0
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package manager

import (
	"fmt"
	"log"
	"sync"

	"github.com/AliyunContainerService/scaler/go/pkg/config"
	"github.com/AliyunContainerService/scaler/go/pkg/model"
	scaler2 "github.com/AliyunContainerService/scaler/go/pkg/scaler"
)

type Pair struct {
	memoryInMb uint64
	runtime    string
}

type SafeMap struct {
	m  map[string]Pair
	mu sync.RWMutex
}

func (s *SafeMap) Set(key string, value Pair) {
	if _, ok := s.Get(key); ok {

		return
	}

	s.mu.Lock()
	defer s.mu.Unlock()
	s.m[key] = value
}

func (s *SafeMap) Get(key string) (Pair, bool) {
	s.mu.RLock()
	defer s.mu.RUnlock()

	value, ok := s.m[key]
	return value, ok
}

type Manager struct {
	rw         sync.RWMutex
	schedulers map[Pair]scaler2.Scaler
	config     *config.Config
	key2Pair   SafeMap
}

func New(config *config.Config) *Manager {
	return &Manager{
		rw:         sync.RWMutex{},
		schedulers: make(map[Pair]scaler2.Scaler),
		key2Pair: SafeMap{
			m:  make(map[string]Pair),
			mu: sync.RWMutex{},
		},
		config: config,
	}
}

func (m *Manager) GetOrCreate(metaData *model.Meta) scaler2.Scaler {
	m.key2Pair.Set(metaData.Key, Pair{metaData.MemoryInMb, metaData.Runtime})
	m.rw.RLock()
	if scheduler := m.schedulers[Pair{metaData.MemoryInMb, metaData.Runtime}]; scheduler != nil {
		m.rw.RUnlock()
		return scheduler
	}
	m.rw.RUnlock()

	m.rw.Lock()
	if scheduler := m.schedulers[Pair{metaData.MemoryInMb, metaData.Runtime}]; scheduler != nil {
		m.rw.Unlock()
		return scheduler
	}
	log.Printf("Create new scaler for app {%s, %s}", metaData.Key, metaData.Runtime)
	scheduler := scaler2.New(metaData, m.config)
	m.schedulers[Pair{metaData.MemoryInMb, metaData.Runtime}] = scheduler
	m.rw.Unlock()
	return scheduler
}

func (m *Manager) Get(metaKey string) (scaler2.Scaler, error) {
	m.rw.RLock()
	defer m.rw.RUnlock()
	pir, ok := m.key2Pair.Get(metaKey)
	if !ok {
		return nil, fmt.Errorf("scaler of app: %s not found", metaKey)
	}
	if scheduler := m.schedulers[pir]; scheduler != nil {
		return scheduler, nil
	}
	return nil, fmt.Errorf("scaler of app: %s not found", metaKey)
}
