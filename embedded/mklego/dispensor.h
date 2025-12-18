#ifndef DISPENSOR_H
#define DISPENSOR_H

#include "lgcode.h"

#define MAX_NUMBER_DISPENSORS 8
#define WIDTH_2X2          3
#define MAX_POSITION_2X2   25
#define WIDTH_2X3          4
#define MAX_POSITION_2X3   24
#define WIDTH_2X4          5
#define MAX_POSITION_2X4   23

struct dispensor {
  int     pos = -1;
  int     brick_id = -1;
  int     status = 1; // 0=blocked/empty, 1=ready/pass, -1=not connected
};

/* Dispensor getters */
const dispensor* get_dispensor       (int dispensor_id);
int              get_dispensor_width (int dispensor_id);
int              find_non_empty_dispensor_with_brick (int brick_id);
bool             dispensor_is_empty  (int dispensor_id);
bool             is_position_occupied(int pos);

/* Dispensor setters */
void set_dispensor_brick (int dispensor_id, int  brick_id);
void set_dispensor_pos   (int dispensor_id, int  pos);
void set_dispensor_status(int dispensor_id, int status);

/* Position Utils */
bool is_legal_placement  (int pos, int width);

#endif