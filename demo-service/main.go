package main

import (
	"fmt"
	"log"
	"net/http"
	"sync"
	"time"
)

func main() {
	go runStateMachine()

	http.HandleFunc("/actuator/health", healthHandler)
	if err := http.ListenAndServe(":8080", nil); err != nil {
		log.Fatalf("server failed: %v", err)
	}
}

type State int

const (
	Healthy State = iota
	Degraded
	Down
	Recovery
)

var (
	currentState State
	mu           sync.Mutex
)

func runStateMachine() {
	cycle := []struct {
		state    State
		duration time.Duration
	}{
		{Healthy, time.Minute * 2},
		{Degraded, time.Minute * 1},
		{Down, time.Second * 90},
		{Recovery, time.Second * 30},
	}

	for {
		for _, step := range cycle {
			mu.Lock()
			currentState = step.state
			mu.Unlock()

			time.Sleep(step.duration)
		}
	}
}

func healthHandler(w http.ResponseWriter, r *http.Request) {
	mu.Lock()
	state := currentState
	mu.Unlock()

	if state == Degraded {
		time.Sleep(time.Second * 2)
	}

	w.Header().Set("Content-Type", "application/json")

	if state == Down {
		w.WriteHeader(http.StatusServiceUnavailable)
		_, _ = fmt.Fprintf(w, `{"status":"DOWN"}`) //nothing to recover
		return
	}

	_, _ = fmt.Fprintf(w, `{"status":"UP"}`)
}
