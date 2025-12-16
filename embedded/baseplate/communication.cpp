
#include "config.h"
#include "communication.h"
#include <HardwareSerial.h>
#include <ESP32SPISlave.h>

constexpr int EVENT_BUFFER_SIZE = 512;
constexpr int PAYLOAD_SIZE      = 3;
constexpr int TOTAL_BUFFER_SIZE = EVENT_BUFFER_SIZE * PAYLOAD_SIZE;

volatile int lftPtr = 0;
volatile int rgtPtr = 0;

DMA_ATTR unsigned char eventBuffer[TOTAL_BUFFER_SIZE];

const int QUEUE_SIZE = 1;

ESP32SPISlave slave;

int nxtPtr (int ptr) {
  return (ptr + 3) % TOTAL_BUFFER_SIZE;
}

void putEvent (struct Event event) {
#ifdef DEBUG
  if (nxtPtr(rgtPtr) == lftPtr) {
    Serial.print("DELAY EVENT STORAGE, COLLISION nxtPtr(rgtPtr)=");
    Serial.println(nxtPtr(rgtPtr));
    Serial.print("BUFFER USAGE: [");
    Serial.print(lftPtr);
    Serial.print("; ");
    Serial.print(rgtPtr);
    Serial.println("]");
  }
#endif
  while (nxtPtr(rgtPtr) == lftPtr)
    vTaskDelay(1);
  
#ifdef DEBUG
  Serial.print("STORING IN BUFFER at pos=");
  Serial.println(rgtPtr);
  Serial.print("BUFFER USAGE: [");
  Serial.print(lftPtr);
  Serial.print("; ");
  Serial.print(rgtPtr);
  Serial.println("]");
#endif
  showEvent(event);
  putInBuffer(event, &eventBuffer[rgtPtr]);

  rgtPtr = nxtPtr(rgtPtr);
}

void sendEventTask (void* taskParameters) {
  slave.setDataMode(SPI_MODE0);
  slave.setQueueSize(QUEUE_SIZE);

  slave.begin(HSPI);

  #ifdef DEBUG
  Serial.println("Starting Event Task");
  #endif

  for (;;) {
    while (lftPtr == rgtPtr) {
      vTaskDelay(1);
    }

    #ifdef DEBUG
    Serial.print("Transfering lftPtr=");
    Serial.println(lftPtr);
    Serial.print((int) eventBuffer[lftPtr]);
    Serial.print(" ");
    Serial.print((int) eventBuffer[lftPtr + 1]);
    Serial.print(" ");
    Serial.println((int) eventBuffer[lftPtr + 2]);
    #endif
    
    slave.transfer(&eventBuffer[lftPtr], NULL, 1);
    lftPtr = nxtPtr(lftPtr);
  }
}
