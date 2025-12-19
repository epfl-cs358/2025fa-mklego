
// ############################
// ######## CONSTANTS #########
// ############################

#define CAN_DISABLE_MOTORS true

#define X_STEP_PIN 3
#define X_DIR_PIN 2
#define Y_STEP_PIN 5
#define Y_DIR_PIN 4
#define Z_STEP_PIN 7
#define Z_DIR_PIN 6
#define R_STEP_PIN 9
#define R_DIR_PIN 8

#define X_MICRO 38
#define Y_MICRO 42
#define Z_MICRO 40
#define R_MICRO 36

#define XY_LEGO_WIDTH 800

#define ENABLE_PIN 32
#define SERVO_PIN 30

#define MOVE_SPEED 3000
#define MOVE_SPEED_Z 3000
#define ROTATION_SPEED 50
#define WIGGLE_SPEED 500

#define CALIBRATION_XYZ_SPEED1 5000.0
#define CALIBRATION_XYZ_SPEED2 200
#define CALIBRATION_R_SPEED1 100.0
#define CALIBRATION_R_SPEED2 50

#define R_DELTA -49
#define ROTATION_90 50

#define NOZZLE_UP 0
#define NOZZLE_DOWN 120
#define NOZZLE_DELAY 200
#define TOP_WIGGLE_RADIUS 50
#define BOTTOM_WIGGLE_RADIUS 30

void initPhysics ();

// #############################
// ######## CALIBRATION ########
// #############################

void calibrateX ();
void calibrateY ();
void calibrateZ ();
void calibrateR (long rotation);

void calibrateAll ();

// ############################
// ########## NOZZLE ##########
// ############################

void nozzleUp   ();
void nozzleDown ();

bool rotateNozzle(int rot);

void rotateRight ();
void rotateLeft ();

// ############################
// ######### MOVEMENT #########
// ############################

struct Referential {
private:
  long originx, originy, originz;
  
  long minx, miny, minz;
  long maxx, maxy, maxz;
  long scax, scay, scaz;

  long wiggleRadius;
public:
  Referential (
    long originx, long originy, long originz,
    
    long minx, long miny, long minz,
    long maxx, long maxy, long maxz,
    long scax, long scay, long scaz,

    long wiggleRadius
  );

  bool moveTo (long x, long y, long z);

  bool wiggle (long x, long y, long z);
};

Referential& dispensorDownReferential ();
Referential& dispensorMoveReferential ();
Referential& plateDownReferential ();
Referential& plateWiggleReferential ();
Referential& plateMoveReferential ();
