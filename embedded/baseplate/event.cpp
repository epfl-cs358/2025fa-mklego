
#include "config.h"
#include "event.h"

#include <HardwareSerial.h>

struct Event createDispensorEvent (unsigned char dispensor, int analog) {
  struct Event result;
  result.eventKind = DISPENSOR_CREATE;
  result.dispensor = dispensor;
  result.params    = analog;

  return result;
}
struct Event removeDispensorEvent (unsigned char dispensor) {
  struct Event result;
  result.eventKind = DISPENSOR_REMOVE;
  result.dispensor = dispensor;

  return result;
}
struct Event setStatusEvent (unsigned char dispensor, enum DispensorStatus status) {
  struct Event result;
  result.eventKind = DISPENSOR_STATUS;
  result.dispensor = dispensor;
  result.params    = (int) status;

  return result;
}

void putInBuffer (struct Event event, unsigned char* buffer) {
  buffer[0] = (unsigned char) (((event.eventKind << 4) | (event.dispensor)) & 0xFF);
  buffer[1] = (unsigned char) ((event.params >> 8) & 0xFF);
  buffer[2] = (unsigned char) (event.params & 0xFF);
}


void showEvent (struct Event event) {
#ifdef DEBUG
  switch (event.eventKind) {
    case DISPENSOR_CREATE: {
      Serial.print("CREATE: Dispensor#");
      Serial.print(event.dispensor);
      Serial.print(" analog=");
      Serial.println(event.params);
      break ;
    }
    case DISPENSOR_REMOVE: {
      Serial.print("REMOVE: Dispensor#");
      Serial.println(event.dispensor);
      break ;
    }
    case DISPENSOR_STATUS: {
      Serial.print("STATUS: Dispensor#");
      Serial.print(event.dispensor);
      Serial.print(" status=");
      switch (event.params) {
        case BLOCKED: {
          Serial.println("BLOCKED");
          break ;
        }
        case PASS: {
          Serial.println("PASS");
          break ;
        }
        default: {
          Serial.println("UNKNOWN");
          break ;
        }
      }
      break ;
    }
    default: {
      Serial.print("UNKNOWN EVENT KIND: ");
      Serial.println((int) event.eventKind);

      break;
    }
  }
#endif
}
