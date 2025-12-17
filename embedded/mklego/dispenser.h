#ifndef DISPENSER_H
#define DISPENSER_H

#include "lgcode.h"

#define MAX_NUMBER_DISPENSERS 9

struct dispenser {
  int           pos;
  int           width;
  bool          empty;
  brick_type    brick;
};

const dispenser* get_dispensers_it  (int dispenser_nmb);
const dispenser* get_dispenser      (uint8_t brick_id);
int              place_dispenser    (dispenser disp);
int              remove_dispenser   (dispenser disp);
bool             is_legal_placement (int pos, int width);

#endif