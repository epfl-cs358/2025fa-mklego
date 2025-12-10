
#include "HardwareSerial.h"

#include "lgcode.h"

struct MSerial {
  template<typename... Args>
  void print(Args... args) {}
  template<typename... Args>
  void println(Args... args) {}
};
#define VSerial MSerial()

/*******************************
 **** Brick Color Container ****
 *******************************/

const int MAX_NUMBER_COLORS = 16;

static int         fx_number_colors = 0;
static uint8_t     fx_color_ids[MAX_NUMBER_COLORS];
static brick_color fx_brick_colors[MAX_NUMBER_COLORS];

int get_number_color () {
  return fx_number_colors;
} 
const uint8_t* get_colors_uuids () {
  return fx_color_ids;
}
const brick_color* get_color (uint8_t color_id) {
  return (const brick_color*) (fx_brick_colors + color_id);
}

void put_color (uint8_t color_id, brick_color color) {
  int idx;
  for (idx = 0; idx < fx_number_colors; idx ++) {
    if (fx_color_ids[idx] == color_id) {
      break ;
    }
  }

  fx_color_ids[idx] = color_id;

  if (idx == fx_number_colors) {
    fx_number_colors ++;
  }
  
  fx_brick_colors[color_id] = color;
}

void reset_colors () {
  fx_number_colors = 0;
}

/******************************
 **** Brick Type Container ****
 ******************************/

const int MAX_NUMBER_TYPES = 16;

static int        fx_number_types = 0;
static uint8_t    fx_types_ids[MAX_NUMBER_TYPES];
static brick_type fx_brick_types[MAX_NUMBER_TYPES];

int get_number_types () {
  return fx_number_types;
}
const uint8_t* get_types_uuids () {
  return fx_types_ids;
}
const brick_type* get_type (uint8_t type_id) {
  return (const brick_type*) (fx_brick_types + type_id);
}

void put_type (uint8_t type_id, brick_type type) {
  int idx;
  for (idx = 0; idx < fx_number_types; idx ++) {
    if (fx_types_ids[idx] == type_id) {
      break ;
    }
  }

  fx_types_ids[idx] = type_id;

  if (idx == fx_number_types) {
    fx_number_types ++;
  }
  
  fx_brick_types[type_id] = type;
}

void reset_types () {
  fx_number_types = 0;
}

/******************************
 **** Plate Size Container ****
 ******************************/

static bool fx_plate_size_configured = false;

static int fx_plate_size_x;
static int fx_plate_size_y;

bool plate_size_configured () {
  return fx_plate_size_configured;
}

int get_plate_size_x () {
  return fx_plate_size_x;
}
int get_plate_size_y () {
  return fx_plate_size_y;
}

void configure_plate_size (int plate_size_x, int plate_size_y) {
  fx_plate_size_configured = true;

  fx_plate_size_x = plate_size_x;
  fx_plate_size_y = plate_size_y;
}

void reset_plate () {
  fx_plate_size_configured = false;
}

/*******************************
 PRINT SECTION
 ********************************/

bool          fx_has_current_operation = false;
OperationType fx_current_operation_type;
uint8_t       fx_current_operation[16];

void pop_current_operation () {
  fx_has_current_operation = false;
  force_parsing ();
}
bool has_current_operation () {
  return fx_has_current_operation;
}
OperationType current_operation_type () {
  return fx_current_operation_type;
}

GrabOperation get_grab_operation () {
  return *((GrabOperation*) fx_current_operation);
}
MoveOperation get_move_operation () {
  return *((MoveOperation*) fx_current_operation);
}
RotateOperation get_rotate_operation () {
  return *((RotateOperation*) fx_current_operation);
}
DropOperation get_drop_operation () {
  return *((DropOperation*) fx_current_operation);
}

/*******************************
 ******* LG Code Reading *******
 *******************************/

enum FSM {
  READ_HEADER,

  READ_COMMAND_KIND,

  READ_SET_SECTION,

  READ_ADD_COLOR_URGBA,
  READ_ADD_COLOR_NAME_HEADER,
  READ_ADD_COLOR_NAME_STRING,
  READ_ADD_COLOR_DESC_HEADER,
  READ_ADD_COLOR_DESC_STRING,

  READ_ADD_BRICK_UUID,
  READ_ADD_BRICK_NAME_HEADER,
  READ_ADD_BRICK_NAME_STRING,
  READ_ADD_BRICK_COLOR_RESISTOR,

  READ_PLATE_SIZE_XY,

  READ_GRAB_BRICK,
  READ_DROP_BRICK,
  READ_MOVE_BRICK,
  READ_ROTATE_BRICK,

  WAIT_POP_OPERATION,
  ERROR_PARSING
};

const int PARSE_BUFFER_SIZE = 256;

FSM parsing_state;
uint8_t parse_buffer[PARSE_BUFFER_SIZE];
int parse_buffer_offset;
int parse_buffer_remaining;

bool fx_had_error = false;
char* fx_error_text = "";

bool fx_in_config_section = false;
bool fx_in_print_section = false;

bool had_error () { return fx_had_error; }
const char* error_text () { return fx_error_text; }
bool in_config_section () { return fx_in_config_section; }
bool in_print_section () { return fx_in_print_section; }

int string_to_read_dne = 0;
int string_to_read_tot = 0;

void reset_parser () {
  parsing_state = READ_HEADER;
  parse_buffer_offset = 0;
  parse_buffer_remaining = 0;

  fx_had_error = false;
  fx_error_text = "";
  fx_in_config_section = false;
  fx_in_print_section = false;
}

int pop_from_buffer (int size) {
  int idx;
  VSerial.print("POP ");
  for (idx = 0; idx < size && parse_buffer_remaining > 0; idx ++) {
    VSerial.print((char) parse_buffer[parse_buffer_offset]);
    parse_buffer_offset ++;
    parse_buffer_remaining --;

    if (parse_buffer_offset == PARSE_BUFFER_SIZE)
      parse_buffer_offset = 0;
  }
  VSerial.println();

  return idx;
}
bool read_from_buffer (uint8_t* buffer, int size) {
  if (size > parse_buffer_remaining) return false;
  for (int idx = 0; idx < size; idx ++) {
    buffer[idx] = parse_buffer[parse_buffer_offset];

    parse_buffer_offset ++;
    parse_buffer_remaining --;

    if (parse_buffer_offset == PARSE_BUFFER_SIZE)
      parse_buffer_offset = 0;
  }
  return true;
}

#define ON_ERROR(msg) { \
    parsing_state = ERROR_PARSING; \
    fx_had_error = true; \
    fx_error_text = msg; \
  }
#define TEST_CMD_KIND(value, tstate, feasible, errmsg) if (cmd_kind == value) { \
    if (feasible) {\
      parsing_state = tstate; \
    } else { \
      ON_ERROR(errmsg); \
    } \
  }
int parse_integer (unsigned char* buffer) {
  return (((int) buffer[0]) << 24)
       | (((int) buffer[1]) << 16)
       | (((int) buffer[2]) << 8)
       | (((int) buffer[3]) << 0);
}
int parse_variadic_integer (unsigned char* buffer, int size) {
  int res = 0;
  for (int i = 0; i < size; i ++) {
    res *= 10;
    res += buffer[i] - '0';
  }

  return res;
}

int8_t      partial_uuid;
brick_color partial_color;
brick_type  partial_type;
void force_parsing () {
  unsigned char stbuf[32];

  bool should_continue = true;
  while (should_continue) {
    VSerial.print("CURRENTLY IN STATE");
    switch (parsing_state) {
      /* -------------------------------------------------------------------------------------------------------------- */
      /* --------------------------------------------------- HEADER --------------------------------------------------- */
      /* -------------------------------------------------------------------------------------------------------------- */
      case READ_HEADER:
        VSerial.println("READ_HEADER");
        should_continue &= read_from_buffer(stbuf, 8);
        if (should_continue) {
          if (stbuf[0] != 'B'
           || stbuf[1] != '-'
           || stbuf[2] != 'L'
           || stbuf[3] != 'G'
           || stbuf[4] != 'C'
           || stbuf[5] != 'O'
           || stbuf[6] != 'D'
           || stbuf[7] != 'E') {
            ON_ERROR("Invalid LG-CODE MAGIC, expected B-LGCODE");
          } else {
            parsing_state = READ_COMMAND_KIND;
          }
        }
        break ;
      
      /* -------------------------------------------------------------------------------------------------------------- */
      /* -------------------------------------------------- CMD KIND -------------------------------------------------- */
      /* -------------------------------------------------------------------------------------------------------------- */

      case READ_COMMAND_KIND:
        VSerial.println("READ_COMMAND_KIND");
        should_continue &= read_from_buffer(stbuf, 1);
        if (should_continue) {
          uint8_t cmd_kind = stbuf[0];

               TEST_CMD_KIND(0, READ_SET_SECTION, true, "")
          else TEST_CMD_KIND(1, READ_ADD_COLOR_URGBA, fx_in_config_section, "addcolor should be in .config section")
          else TEST_CMD_KIND(2, READ_ADD_BRICK_UUID,  fx_in_config_section, "addbrick should be in .config section")
          else TEST_CMD_KIND(3, READ_PLATE_SIZE_XY,   fx_in_config_section, "platesize should be in .config section")
          else TEST_CMD_KIND(4, READ_GRAB_BRICK,      fx_in_print_section,  "grabbrick should be in .print section")
          else TEST_CMD_KIND(5, READ_DROP_BRICK,      fx_in_print_section,  "dropbrick should be in .print section")
          else TEST_CMD_KIND(6, READ_ROTATE_BRICK,    fx_in_print_section,  "rotate should be in .print section")
          else TEST_CMD_KIND(7, READ_MOVE_BRICK,      fx_in_print_section,  "move should be in .print section")
          else ON_ERROR("Could not recognize command kind.");
        }
        break ;
      
      /* -------------------------------------------------------------------------------------------------------------- */
      /* ------------------------------------------------- SETSECTION ------------------------------------------------- */
      /* -------------------------------------------------------------------------------------------------------------- */
      case READ_SET_SECTION:
        VSerial.println("READ_SET_SECTION");
        should_continue &= read_from_buffer(stbuf, 1);
        if (should_continue) {
          VSerial.println((int) stbuf[0]);
          if (fx_in_print_section) ON_ERROR("Cannot change section after .print")
          else if (fx_in_config_section && stbuf[0] == 1) {
            fx_in_config_section = false;
            fx_in_print_section = true;
            parsing_state = READ_COMMAND_KIND;
          } else if (!fx_in_config_section && stbuf[0] == 0) {
            fx_in_config_section = true;
            parsing_state = READ_COMMAND_KIND;
          } else ON_ERROR("Could not recognize section");
        }
        break ;

      /* -------------------------------------------------------------------------------------------------------------- */
      /* ----------------------------------------------- CONFIG SECTION ----------------------------------------------- */
      /* -------------------------------------------------------------------------------------------------------------- */
      case READ_ADD_COLOR_URGBA:
        VSerial.println("READ_ADD_COLOR_URGBA");
        should_continue &= read_from_buffer(stbuf, 5);
        if (should_continue) {
          partial_uuid = stbuf[0];
          partial_color.red = stbuf[1];
          partial_color.green = stbuf[2];
          partial_color.blue = stbuf[3];
          partial_color.alpha = stbuf[4];
          parsing_state = READ_ADD_COLOR_NAME_HEADER;
        }
        break ;
      case READ_ADD_COLOR_NAME_HEADER:
        VSerial.println("READ_ADD_COLOR_NAME_HEADER");
        should_continue &= read_from_buffer(stbuf, 4);
        if (should_continue) {
          string_to_read_dne = 0;
          string_to_read_tot = parse_integer(stbuf);
          parsing_state = READ_ADD_COLOR_NAME_STRING;
        }
        break ;
      case READ_ADD_COLOR_NAME_STRING:
        VSerial.println("READ_ADD_COLOR_NAME_STRING");
        if (string_to_read_dne == 0) {
          int tar = string_to_read_tot;
          if (tar > COLOR_NAME_MAX_SIZE) tar = COLOR_NAME_MAX_SIZE;

          should_continue &= read_from_buffer(stbuf, tar);
          if (should_continue) {
            string_to_read_dne = tar;
            for (int idx = 0; idx < tar; idx ++) {
              partial_color.name[idx] = stbuf[idx];
            }
            partial_color.name[tar] = (char) 0;
          }
        } else {
          string_to_read_dne += pop_from_buffer(string_to_read_tot - string_to_read_dne);
          if (string_to_read_dne != string_to_read_tot) {
            should_continue = false;
          }
        }
        if (string_to_read_dne == string_to_read_tot) {
          parsing_state = READ_ADD_COLOR_DESC_HEADER;
          should_continue = true;
        }
        break ;
      case READ_ADD_COLOR_DESC_HEADER:
        VSerial.println("READ_ADD_COLOR_DESC_HEADER");
        should_continue &= read_from_buffer(stbuf, 4);
        if (should_continue) {
          string_to_read_dne = 0;
          string_to_read_tot = parse_integer(stbuf);
          parsing_state = READ_ADD_COLOR_DESC_STRING;
        }
        break ;
      case READ_ADD_COLOR_DESC_STRING:
        VSerial.println("READ_ADD_COLOR_DESC_STRING");
        VSerial.println(parse_buffer_remaining);
        VSerial.println(string_to_read_tot - string_to_read_dne);
        string_to_read_dne += pop_from_buffer(string_to_read_tot - string_to_read_dne);
        VSerial.println(string_to_read_tot - string_to_read_dne);
        VSerial.println(parse_buffer_remaining);

        should_continue = string_to_read_dne == string_to_read_tot;
        if (should_continue) {
          put_color(partial_uuid, partial_color);
          parsing_state = READ_COMMAND_KIND;
        }
        break;

      case READ_ADD_BRICK_UUID:
        VSerial.println("READ_ADD_BRICK_UUID");
        should_continue &= read_from_buffer(stbuf, 1);
        if (should_continue) {
          parsing_state = READ_ADD_BRICK_NAME_HEADER;
          partial_uuid = stbuf[0];
        }
        break ;
      case READ_ADD_BRICK_NAME_HEADER:
        VSerial.println("READ_ADD_BRICK_NAME_HEADER");
        should_continue &= read_from_buffer(stbuf, 4);
        if (should_continue) {
          string_to_read_dne = 0;
          string_to_read_tot = parse_integer(stbuf);
          parsing_state = READ_ADD_BRICK_NAME_STRING;
          if (string_to_read_tot >= 32) {
            ON_ERROR("Brick name is too long.");
          }
          if (string_to_read_tot < 7) {
            ON_ERROR("Brick name is too short.");
          }
        }
        break ;
      case READ_ADD_BRICK_NAME_STRING:
        VSerial.println("READ_ADD_BRICK_NAME_STRING");
        should_continue &= read_from_buffer(stbuf, string_to_read_tot);
        if (should_continue) {
          parsing_state = READ_ADD_BRICK_COLOR_RESISTOR;
          if (stbuf[0] != 's' || stbuf[1] != 't' || stbuf[2] != 'd' || stbuf[3] != '_') {
            ON_ERROR("Invalid brick name");
          } else {
            int ct = 0;
            int cn = 0;
            int cw = 0;
            int cr = 0;
            for (int i = 4; i < string_to_read_tot; i ++) {
              if ('0' <= stbuf[i] && stbuf[i] <= '9') {
                ct ++;
              } else if (stbuf[i] == '_') {
                cn = i;
                cr ++;
              } else {
                cw ++;
              }
            }
            if (cw != 0 || cr != 1 || cn == 4 || cn == string_to_read_tot - 1) {
              ON_ERROR("Invalid brick name");
            } else {
              partial_type.size_x = parse_variadic_integer(stbuf + 4, cn - 4);
              partial_type.size_y = parse_variadic_integer(stbuf + cn + 1, string_to_read_tot - (cn + 1));
            }
          }
        }
        break ;
      case READ_ADD_BRICK_COLOR_RESISTOR:
        VSerial.println("READ_ADD_BRICK_COLOR_RESISTOR");
        should_continue &= read_from_buffer(stbuf, 5);
        if (should_continue) {
          partial_type.color = (int) stbuf[0];
          partial_type.resistor = parse_integer(stbuf + 1);

          put_type(partial_uuid, partial_type);
          parsing_state = READ_COMMAND_KIND;
        }
        break ;

      case READ_PLATE_SIZE_XY:
        VSerial.println("READ_PLATE_SIZE_XY");
        should_continue &= read_from_buffer(stbuf, 2);
        if (should_continue) {
          configure_plate_size((int) stbuf[0], (int) stbuf[1]);
          parsing_state = READ_COMMAND_KIND;
        }

        break ;

      /* ------------------------------------------------------------------------------------------------------------- */
      /* ----------------------------------------------- PRINT SECTION ----------------------------------------------- */
      /* ------------------------------------------------------------------------------------------------------------- */

      case READ_MOVE_BRICK:
        VSerial.println("READ_MOVE_BRICK");
        should_continue &= read_from_buffer(stbuf, 3);
        if (should_continue) {
          MoveOperation* op = (MoveOperation*) fx_current_operation;
          op->x = stbuf[0];
          op->y = stbuf[1];
          op->z = stbuf[2];

          fx_has_current_operation  = true;
          fx_current_operation_type = MOVE;
          parsing_state = WAIT_POP_OPERATION;
        }
        break ;
      case READ_GRAB_BRICK:
        VSerial.println("READ_GRAB_BRICK");
        should_continue &= read_from_buffer(stbuf, 2);
        if (should_continue) {
          GrabOperation* op = (GrabOperation*) fx_current_operation;
          op->brick_id = stbuf[0];
          op->attachment_id = stbuf[1];

          fx_has_current_operation  = true;
          fx_current_operation_type = GRAB;
          parsing_state = WAIT_POP_OPERATION;
        }
        break ;
      case READ_ROTATE_BRICK:
        VSerial.println("READ_ROTATE_BRICK");
        should_continue &= read_from_buffer(stbuf, 1);
        if (should_continue) {
          RotateOperation* op = (RotateOperation*) fx_current_operation;
          op->rotation = stbuf[0];
          if (stbuf[0] == 255)
            op->rotation = -1;

          fx_has_current_operation  = true;
          fx_current_operation_type = ROTATE;
          parsing_state = WAIT_POP_OPERATION;
        }
        break ;
      case READ_DROP_BRICK:
        VSerial.println("READ_DROP_BRICK");
        fx_has_current_operation  = true;
        fx_current_operation_type = DROP;
        parsing_state = WAIT_POP_OPERATION;
        break ;

      case WAIT_POP_OPERATION:
        VSerial.println("WAIT_POP_OPERATION");
        if (fx_has_current_operation) {
          should_continue = false;
        } else {
          parsing_state = READ_COMMAND_KIND;
        }
        break;

      case ERROR_PARSING:
      default:
        VSerial.println("ERROR_PARSING");
        should_continue = false;
        break ;
    }
  };
}

void reset_lgcode () {
  reset_parser();
  reset_colors();
  reset_types ();
  reset_plate ();
}

size_t write_lgcode (uint8_t* lgcode, size_t content) {
  size_t written = content;
  if (lgcode_buffer_size() < written) written = lgcode_buffer_size();

  for (int idx = 0; idx < written; idx ++) {
    int jdx = parse_buffer_offset + parse_buffer_remaining;
    if (jdx >= PARSE_BUFFER_SIZE) jdx -= PARSE_BUFFER_SIZE;

    parse_buffer_remaining ++;
    parse_buffer[jdx] = lgcode[idx];
  }

  force_parsing ();

  return written;
}
size_t lgcode_buffer_size () {
  return PARSE_BUFFER_SIZE - parse_buffer_remaining;
}