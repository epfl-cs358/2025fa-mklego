
#include "config.h"
#include "communication.h"
#include <Arduino.h>

const int sckPin = 10;
const int datPin = 11;
const int avlPin = 12;

const int BUFFER_SIZE = 512;

unsigned char rotBuffer[BUFFER_SIZE];

volatile int lftPtr = 0;
volatile int lftOff = 7;
volatile int rgtPtr = 0;
void updateAvailability () {
  digitalWrite(avlPin, rgtPtr == lftPtr ? LOW : HIGH);
}
int pollBit () {
  if (lftPtr == rgtPtr) return 0;
  int res = (rotBuffer[lftPtr] >> lftOff) & 1;

  lftOff --;
  if (lftOff < 0) {
    lftOff = 7;
    lftPtr ++;
    if (lftPtr == BUFFER_SIZE) {
      lftPtr = 0;
    }
  }

  updateAvailability();
  return res;
}
void pushByte (unsigned char byte) {
  int nxtRgt = rgtPtr + 1;
  if (nxtRgt == BUFFER_SIZE) nxtRgt = 0;

  while (nxtRgt == lftPtr) {
    delayMicroseconds(1);
  }

  rotBuffer[rgtPtr] = byte;
  rgtPtr = nxtRgt;
  updateAvailability();
}

int globalClockCnt = 0;
void onClockRising () {
  digitalWrite(datPin, pollBit());
}

void slave_transmit (unsigned char *buffer, int size) {
  unsigned char csum = 0;
  for (int i = 0; i < size; i ++) {
    pushByte(buffer[i]);
    pushByte(buffer[i]);
    pushByte(buffer[i]);
    csum ^= buffer[i];
  }

  pushByte(csum);
  pushByte(csum);
  pushByte(csum);
}

void putEvent (struct Event event) {
  showEvent(event);

  unsigned char buffer[3];
  putInBuffer(event, buffer);
  slave_transmit(buffer, 3);
}

void setupEventTask () {
    pinMode(sckPin, INPUT);
    pinMode(datPin, OUTPUT);
    pinMode(avlPin, OUTPUT);
    digitalWrite(avlPin, LOW);
    attachInterrupt(digitalPinToInterrupt(sckPin), &onClockRising, RISING);
}
