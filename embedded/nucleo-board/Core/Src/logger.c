/*
 * logger.c
 *
 *  Created on: Nov 9, 2025
 *      Author: theoh
 */

#include "main.h"
#include <stdio.h>

#ifdef __GNUC__
  /* With GCC/G++ define the character output function */
  int __io_putchar(int ch)
#else
  int fputc(int ch, FILE *f)
#endif
{
  // Replace huartX with the handle of your configured UART (e.g., huart2)
  HAL_UART_Transmit(get_huart2(), (uint8_t *)&ch, 1, HAL_MAX_DELAY);
  return ch;
}
