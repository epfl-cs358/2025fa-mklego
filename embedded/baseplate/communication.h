
#ifndef COMMUNICATION_H
#define COMMUNICATION_H

#include "event.h"

void putEvent (struct Event event);
void sendEventTask (void* taskParameters);

#endif /* COMMUNICATION_H */
