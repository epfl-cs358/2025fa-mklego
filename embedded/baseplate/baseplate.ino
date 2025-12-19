
#include "communication.h"
#include <IRremote.h>

#define NB_DISPENSORS 8
#define IR_LED 21

struct Dispensor {
  int digitalPin;
  int analogPin;
  int status;
};

static Dispensor listDisp[NB_DISPENSORS] = {
  {35, 15, NOT_CONNECTED},
  {42, 16, NOT_CONNECTED},
  {41, 17, NOT_CONNECTED},
  {40, 8, NOT_CONNECTED},
  {36, 7, NOT_CONNECTED},
  {37, 6, NOT_CONNECTED},
  {38, 5, NOT_CONNECTED},
  {39, 4, NOT_CONNECTED}
};

void setup() {
  Serial.begin(9600);

  setupEventTask();

  pinMode(IR_LED, OUTPUT);

  IrSender.begin(IR_LED, ENABLE_LED_FEEDBACK);
  IrSender.enableIROut(38);

  for (int i = 0; i < 8; i++) {
    pinMode(listDisp[i].digitalPin, INPUT_PULLUP);
  }
}

void loop() {
  checkDispensors();
}

int smoothAnalogRead (int pin) {
  long total = 0;
  for (int i= 0; i < 256; i++) {
    total += analogRead(pin);
  }
  return total / 256;
}

DispensorStatus smoothIRRead (int pin) {
  while (1) {
    int nb_pass = 0;
    for(int i = 0; i < 20; i++) {
      uint16_t rawSignal[] = {1000};
      IrSender.sendRaw(rawSignal, 1, 38);
      if (digitalRead(pin) == LOW) {
        nb_pass++;
      }
    }

    if (nb_pass > 5) {
      return PASS;
    } else if (nb_pass == 0) {
      return BLOCKED;
    }
  }
}

void checkDispensors() {

  for (int i = 0; i < 8; i++) {
    int analogValue = smoothAnalogRead(listDisp[i].analogPin);
    // DispensorStatus status = smoothIRRead(listDisp[i].digitalPin);

    if (analogValue < 4000 && listDisp[i].status == NOT_CONNECTED) {
      delay(500);
      analogValue = smoothAnalogRead(listDisp[i].analogPin);

      // Add dispensor
      putEvent( createDispensorEvent(i, analogValue) );
      listDisp[i].status = status;
      //putEvent( setStatusEvent(i, FULL) );
    } else if (analogValue >= 4000 && listDisp[i].status != NOT_CONNECTED) {
      // Remove dispensor
      putEvent( removeDispensorEvent(i) );
      listDisp[i].status = NOT_CONNECTED;
    }

    /*
    if (status != listDisp[i].status && analogValue < 4000) {
      // Update status
      listDisp[i].status = status;
      putEvent( setStatusEvent(i, status) );
    }
    */
  }
}
