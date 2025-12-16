
#include "communication.h"

void setup() {
  Serial.begin(9600);

  xTaskCreatePinnedToCore(
    &sendEventTask,
    "CommunicationTx",
    10'000,
    NULL,
    8,
    NULL,
    0
  );

  putEvent( createDispensorEvent(0, 1000) );
  putEvent( createDispensorEvent(1, 3000) );
  putEvent( setStatusEvent(0, RECEIVER_LOW) );
  putEvent( setStatusEvent(1, RECEIVER_HIGH) );
  putEvent( removeDispensorEvent(0) );
  putEvent( createDispensorEvent(2, 2000) );
  putEvent( setStatusEvent(2, RECEIVER_HIGH) );
  putEvent( setStatusEvent(2, RECEIVER_LOW) );
}

void loop() {}
