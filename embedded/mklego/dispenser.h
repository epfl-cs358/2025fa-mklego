
#include "lgcode.h"

#define uint8_t unsigned char

struct dispenser {
  int           pos;
  int           width;
  bool          empty;
  brick_type    brick;
};

const dispenser* get_dispensers_it (int dispenser_nmb);
const dispenser* get_dispenser     (brick_type brick);
int        add_dispenser     (dispenser disp);
