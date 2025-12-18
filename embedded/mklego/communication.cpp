#include "communication.h"
#include "dispensor.h"
#include "lgcode.h"
#include <Arduino.h>

static int sckPin;
static int datPin;
static int avlPin;

enum EventKind {
  DISPENSOR_CREATE, /* KIND (4) Dispensor (4) Analog Read (16) */
  DISPENSOR_REMOVE, /* KIND (4) Dispensor (4) None (16)*/
  DISPENSOR_STATUS  /* KIND (4) Dispensor (4) Status (16, =0 if disabled, =1 if enabled) */
};
enum DispensorStatus {
  NOT_CONNECTED = -1,
  BLOCKED  = 0,
  PASS = 1
};

void communication_begin(int sck, int dat, int avl) {
  sckPin = sck;
  datPin = dat;
  avlPin = avl;

  pinMode(sckPin, OUTPUT);
  pinMode(datPin, INPUT);
  pinMode(avlPin, INPUT);

  digitalWrite(sckPin, HIGH);
}


int master_read_bit() {
  digitalWrite(sckPin, LOW);
  delayMicroseconds(100);
  digitalWrite(sckPin, HIGH);
  delayMicroseconds(100);

  return digitalRead(datPin);
}


unsigned char master_read_byte() {
  unsigned char res = 0;
  for (int i = 0; i < 8; i++) {
    res = (res << 1) | master_read_bit();
  }
  return res;
}


static int majority(int a, int b, int c) {
  return (a & b) | (b & c) | (a & c);
}


unsigned char master_read_byte_safe() {
  int a = master_read_byte();
  int b = master_read_byte();
  int c = master_read_byte();

  return (unsigned char) majority(a, b, c);
}


bool master_read(unsigned char *buffer, int size) {
  unsigned char csum = 0;

  for (int i = 0; i < size; i++) {
    buffer[i] = master_read_byte_safe();
    csum ^= buffer[i];
  }
  csum ^= master_read_byte_safe();
  delayMicroseconds(10);

  return csum == 0;
}

int get_closest_brick_id (int analog) {
  int id = -1;
  int bid = -1;
  int resistor = 0;
  int closest = 10000;

  for (int i = 0; i < get_number_types(); i++) {
    id = get_types_uuids()[i];
    resistor = get_type(id)->resistor;

    int diff = abs(resistor - analog);
    if (diff < closest) {
      closest = diff;
      bid = id;
    }
  }

  return bid;
}

bool process_event() {
  if (digitalRead(avlPin) == LOW) {
    return false;
  }
  delayMicroseconds(100);
  unsigned char buffer[3];
  int temp = -1;
  Serial.println("NEW READ:");
  if (master_read(buffer, 3)) {
    int eventKind = (buffer[0] >> 4) & 0x0F;
    int dispensor = buffer[0] & 0x0F;
    int params    = (buffer[1] << 8) | buffer[2];
    Serial.print(eventKind);
    Serial.print(" ");
    Serial.print(dispensor);
    Serial.print(" ");
    Serial.println(params);
    temp = eventKind;
    switch (eventKind)
    {
    case DISPENSOR_CREATE: {
        set_dispensor_brick(dispensor, get_closest_brick_id(params));
        set_dispensor_status(dispensor, PASS);
        Serial.print("Dispensor ");
        Serial.print(dispensor);
        Serial.print(" is ");
        Serial.print(get_color(get_type(get_dispensor(dispensor)->brick_id)->color)->red);
        Serial.print(" ");
        Serial.print(get_color(get_type(get_dispensor(dispensor)->brick_id)->color)->green);
        Serial.print(" ");
        Serial.println(get_color(get_type(get_dispensor(dispensor)->brick_id)->color)->blue);
        break;
      }

    case DISPENSOR_REMOVE: {
        set_dispensor_brick(dispensor, -1);
        set_dispensor_status(dispensor, NOT_CONNECTED);
        Serial.print("Dispensor ");
        Serial.print(dispensor);
        Serial.println(" is REMOVED");
        break;
      }

    case DISPENSOR_STATUS: {
        // params is expected to be 0 (blocked/empty) or 1 (pass/ready)
        const int st = (params == 0) ? BLOCKED : PASS;
        set_dispensor_status(dispensor, st);
        Serial.print("Dispensor ");
        Serial.print(dispensor);
        Serial.print(" status: ");
        Serial.println(st);
        break;
      }
      
    default:
      break;
    }

    return true;
  } else {
    Serial.print("Com error: ");
    Serial.println(temp);
    return false;
  }
}