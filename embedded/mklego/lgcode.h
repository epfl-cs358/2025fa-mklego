
#ifndef LGCODE_H
#define LGCODE_H
#define uint8_t unsigned char

/* ============================== */
/* ======= CONFIG SECTION ======= */
/* ============================== */

/**********************************/
/********** BRICK COLORS **********/
/**********************************/

const int COLOR_NAME_MAX_SIZE = 9;

struct brick_color {
  uint8_t red; 
  uint8_t green;
  uint8_t blue;
  uint8_t alpha;

  char name[COLOR_NAME_MAX_SIZE + 1];
};

int                get_number_color ();
const uint8_t*     get_colors_uuids ();
const brick_color* get_color        (uint8_t color_id);

/*********************************/
/********** BRICK TYPES **********/
/*********************************/

struct brick_type {
  uint8_t size_x, size_y;
  uint8_t color;

  int resistor;
};

int               get_number_types ();
const uint8_t*    get_types_uuids  ();
const brick_type* get_type         (uint8_t brick_id);

/**********************************/
/*********** PLATE SIZE ***********/
/**********************************/

bool plate_size_configured ();

int get_plate_size_x ();
int get_plate_size_y ();

/* =============================== */
/* ======== PRINT SECTION ======== */
/* =============================== */

enum OperationType {
  GRAB, MOVE, DROP, ROTATE
};

struct GrabOperation {
  int brick_id;
  int attachment_id;
};

struct MoveOperation {
  int x, y, z;
};

struct RotateOperation {
  int rotation;
};

struct DropOperation {};

void          pop_current_operation ();
bool          has_current_operation ();
OperationType current_operation_type ();

GrabOperation   get_grab_operation ();
MoveOperation   get_move_operation ();
RotateOperation get_rotate_operation ();
DropOperation   get_drop_operation ();

/* ================================ */
/* ======== STREAM SECTION ======== */
/* ================================ */

bool        had_error  ();
const char* error_text ();

bool in_config_section ();
bool in_print_section  ();

void reset_lgcode ();

void   force_parsing ();
size_t write_lgcode (uint8_t* lgcode, size_t content);
size_t lgcode_buffer_size ();

#endif
