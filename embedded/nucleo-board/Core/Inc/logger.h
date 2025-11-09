/*
 * logger.h
 *
 *  Created on: Nov 9, 2025
 *      Author: Théo Hollender
 */

#ifndef INC_LOGGER_H_
#define INC_LOGGER_H_

#include <stdio.h>
#include <stdbool.h>

enum LogLevel {
	LL_DEBUG,
	LL_INFO,
	LL_WARNING,
	LL_DANGER,
	LL_CRITICAL
};

#define CURRENT_LOG_LEVEL LL_DEBUG

bool should_display (enum LogLevel level) {
	return level >= CURRENT_LOG_LEVEL;
}

#define log_debug(module, fmt, ...) if (should_display(LL_DEBUG)) { \
		printf("[DEBUG: " module "] " fmt "\r\n" __VA_OPT__(, __VA_ARGS__)); \
	}
#define log_info(module, fmt, ...) if (should_display(LL_INFO)) { \
		printf("[INFO: " module "] " fmt "\r\n" __VA_OPT__(, __VA_ARGS__)); \
	}
#define log_warning(module, fmt, ...) if (should_display(LL_WARNING)) { \
		printf("[WARNING: " module "] " fmt "\r\n" __VA_OPT__(, __VA_ARGS__)); \
	}
#define log_danger(module, fmt, ...) if (should_display(LL_DANGER)) { \
		printf("[DANGER: " module "] " fmt "\r\n" __VA_OPT__(, __VA_ARGS__)); \
	}
#define log_critical(module, fmt, ...) if (should_display(LL_CRITICAL)) { \
		printf("[CRITICAL: " module "] " fmt "\r\n" __VA_OPT__(, __VA_ARGS__)); \
	}

#endif /* INC_LOGGER_H_ */
