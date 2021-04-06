// See LICENSE.Sifive for license details.
#include <stdbool.h>
#include <stdint.h>

#include <platform.h>

#include "common.h"

#define DEBUG
#include "kprintf.h"

int main(void) {
  REG32(uart, UART_REG_TXCTRL) = UART_TXEN;

  kputs("INIT");

  kputs("BOOTLOOP");

  while (true)
    __asm__ __volatile__("nop");

  // we should not return (returning jumps to memory)
  return 0;
}
