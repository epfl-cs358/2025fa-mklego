
#include "physics.h"

#include <AccelStepper.h>
#include <MultiStepper.h>
#include <Servo.h>

static AccelStepper stepperX(AccelStepper::DRIVER, X_STEP_PIN, X_DIR_PIN);
static AccelStepper stepperY(AccelStepper::DRIVER, Y_STEP_PIN, Y_DIR_PIN);
static AccelStepper stepperZ(AccelStepper::DRIVER, Z_STEP_PIN, Z_DIR_PIN);
static AccelStepper stepperR(AccelStepper::DRIVER, R_STEP_PIN, R_DIR_PIN);

static MultiStepper multi;

static Servo nozzleServo;

void enableMotors () {
  digitalWrite(ENABLE_PIN, LOW);
}
void disableMotors () {
  if (CAN_DISABLE_MOTORS) {
    digitalWrite(ENABLE_PIN, HIGH);
  }
}

void initPhysics () {
  pinMode(X_MICRO, INPUT_PULLUP);
  pinMode(Y_MICRO, INPUT_PULLUP);
  pinMode(Z_MICRO, INPUT_PULLUP);
  pinMode(R_MICRO, INPUT_PULLUP);

  pinMode(ENABLE_PIN, OUTPUT);
  enableMotors ();
  disableMotors();

  nozzleServo.attach(SERVO_PIN);
  nozzleDown ();

  stepperX.setMaxSpeed(MOVE_SPEED);
  stepperY.setMaxSpeed(MOVE_SPEED);
  stepperZ.setMaxSpeed(MOVE_SPEED);
  stepperR.setMaxSpeed(ROTATION_SPEED);
  
  multi.addStepper(stepperX);
  multi.addStepper(stepperY);
  multi.addStepper(stepperZ);
}

void calibrate (int pin, int sign, AccelStepper &stepper, float start_speed, float end_speed) {
  enableMotors();

  stepper.setCurrentPosition(0);
  stepper.moveTo(100000LL);
  stepper.setSpeed(start_speed * sign);
  while (digitalRead(pin) == LOW) {
    stepper.runSpeed();
  }
  stepper.setCurrentPosition(0);
  
  stepper.moveTo(100000LL);
  stepper.setSpeed(- end_speed * sign);
  while (digitalRead(pin) == HIGH) {
    stepper.runSpeed();
  }
  stepper.setCurrentPosition(0);
  
  disableMotors();
}





void calibrateX () {
  calibrate(X_MICRO, -1, stepperX, CALIBRATION_XYZ_SPEED1, CALIBRATION_XYZ_SPEED2);
}
void calibrateY () {
  calibrate(Y_MICRO, -1, stepperY, CALIBRATION_XYZ_SPEED1, CALIBRATION_XYZ_SPEED2);
}
void calibrateZ () {
  calibrate(Z_MICRO, 1, stepperZ, CALIBRATION_XYZ_SPEED1, CALIBRATION_XYZ_SPEED2);
}
void calibrateR () {
  calibrate(R_MICRO, 1, stepperR, CALIBRATION_R_SPEED1, CALIBRATION_R_SPEED2);
  
  stepperR.move(R_DELTA);
  stepperR.setSpeed(ROTATION_SPEED);
  while (stepperR.distanceToGo() != 0) {
    stepperR.runSpeedToPosition();
  }

  stepperR.setCurrentPosition(0);
}
void calibrateAll () {
  calibrateZ();
  calibrateX();
  calibrateR();
  calibrateX();
  calibrateY();
}



void nozzleUp () {
  nozzleServo.write(NOZZLE_UP);
}
void nozzleDown () {
  nozzleServo.write(NOZZLE_DOWN);
}
void moveMotorsTo (long x, long y, long z) {
  long positions[3];
  positions[0] = x;
  positions[1] = y;
  positions[2] = z;

  enableMotors ();
  multi.moveTo(positions);
  multi.runSpeedToPosition();
  disableMotors ();
}
void rotateRight() {
  stepperR.move(ROTATION_90);
  stepperR.setSpeed(ROTATION_SPEED);
  while (stepperR.distanceToGo() != 0) {
    stepperR.runSpeedToPosition();
  }
}

void rotateLeft() {
  stepperR.move(-ROTATION_90);
  stepperR.setSpeed(ROTATION_SPEED);
  while (stepperR.distanceToGo() != 0) {
    stepperR.runSpeedToPosition();
  }
}




Referential::Referential (
  int originx, int originy, int originz,
    
  int minx, int miny, int minz,
  int maxx, int maxy, int maxz,
  int scax, int scay, int scaz
) {
  this->originx = originx;
  this->originy = originy;
  this->originz = originz;

  this->minx = minx; this->miny = miny; this->minz = minz;
  this->maxx = maxx; this->maxy = maxy; this->maxz = maxz;
  this->scax = scax; this->scay = scay; this->scaz = scaz;
}

bool Referential::moveTo (int x, int y, int z) {
  if (x < minx || y < miny || z < minz) return false;
  if (x > maxx || y > maxy || z > maxz) return false;
  
  moveMotorsTo(
    originx + scax * x, 
    originy + scay * y, 
    originz + scaz * z);
}
