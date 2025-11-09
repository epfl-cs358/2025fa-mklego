/*
 * Servomotor.c
 *
 *  Created on: Nov 9, 2025
 *      Author: theoh
 */

extern "C" {
	#include <Servomotor.h>
}

#include <stdio.h>

Servomotor createServomotor (
		TIM_HandleTypeDef* timer,
		int channel,
		PulseRange pulse_range,
		AngleRange angle_range) {
	Servomotor motor;
	motor.timer = timer;
	motor.channel = channel;
	motor.pulse_range = pulse_range;
	motor.angle_range = angle_range;
	return motor;
}
void initServomotor (Servomotor *servo){
	setAngle(servo, servo->angle_range.base);
}
void startServomotor (Servomotor *servo) {
	HAL_TIM_PWM_Start(servo->timer, servo->channel);
}
void setPulse (Servomotor *servo, int pulse) {
	if (pulse < servo->pulse_range.min) pulse = servo->pulse_range.min;
	if (pulse > servo->pulse_range.max) pulse = servo->pulse_range.max;
	if (pulse < 0) pulse = 0;

	__HAL_TIM_SET_COMPARE(servo->timer, servo->channel, pulse);
}
void setAngle (Servomotor *servo, float angle) {
	float da = angle - servo->angle_range.min;
	float ta = ((float) servo->angle_range.max - (float) servo->angle_range.min);

	float sl = da / ta;
	float cr = servo->pulse_range.min + sl * (servo->pulse_range.max - servo->pulse_range.min);

	setPulse(servo, (int) cr);
}

