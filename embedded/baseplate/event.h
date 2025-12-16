
#ifndef EVENT_H
#define EVENT_H

/* Event Kind */

enum EventKind {
  DISPENSOR_CREATE, /* KIND (4) Dispensor (4) Analog Read (16) */
  DISPENSOR_REMOVE, /* KIND (4) Dispensor (4) None (16)*/
  DISPENSOR_STATUS  /* KIND (4) Dispensor (4) Status (16, =0 if disabled, =1 if enabled) */
};
enum DispensorStatus {
  RECEIVER_LOW  = 0,
  RECEIVER_HIGH = 1
};

struct Event {
  enum EventKind eventKind;
  unsigned char dispensor;

  int params;
};

struct Event createDispensorEvent (unsigned char dispensor, int analog);
struct Event removeDispensorEvent (unsigned char dispensor);
struct Event setStatusEvent       (unsigned char dispensor, enum DispensorStatus status);

void putInBuffer (struct Event event, unsigned char* buffer);

void showEvent (struct Event event);

#endif /* EVENT_H */
