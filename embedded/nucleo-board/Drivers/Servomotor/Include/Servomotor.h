/*
 * Servomotor.h
 *
 *  Created on: Nov 9, 2025
 *      Author: Théo Hollender
 */

#ifndef SERVOMOTOR_INCLUDE_SERVOMOTOR_H_
#define SERVOMOTOR_INCLUDE_SERVOMOTOR_H_

#include "stm32f4xx_hal.h"

typedef struct {
	int min;
	int max;
} PulseRange;

typedef struct {
	int min;
	int base;
	int max;
} AngleRange;

typedef struct {
	TIM_HandleTypeDef* timer;
	int channel;

	PulseRange pulse_range;
	AngleRange angle_range;
} Servomotor;

Servomotor createServomotor (
		TIM_HandleTypeDef* timer,
		int channel,
		PulseRange pulse_range,
		AngleRange angle_range);
void initServomotor (Servomotor *servo);
void startServomotor (Servomotor *servo);
void setPulse (Servomotor *servo, int pulse);
void setAngle (Servomotor *servo, float angle);

#endif /* SERVOMOTOR_INCLUDE_SERVOMOTOR_H_ */
