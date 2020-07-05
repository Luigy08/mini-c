        .data
_i:   .word 0
_j:   .word 0
_msg0:  .asciiz "*"
_msg1:  .asciiz "\n"


        .text
        .globl main


main:
  li  $t0,  0
  sw  $t0,  _i

_etiq0:
  lw  $t0,  _i
  li  $t1,  5
  blt  $t0, $t1,  _etiq1
  b _etiq2

_etiq3:
  lw  $t0,  _i
  li  $t1, 1
  add $t2,  $t0, $t1

  sw  $t2,  _i

  b _etiq0

_etiq1:
  li  $t0,  0
  sw  $t0,  _j

_etiq4:
  lw  $t0,  _j
  lw  $t1,  _i
  ble  $t0, $t1,  _etiq5
  b _etiq6

_etiq7:
  lw  $t0,  _j
  li  $t1, 1
  add $t2,  $t0, $t1

  sw  $t2,  _j

  b _etiq4

_etiq5:
  li  $v0,  4
  la  $a0,  _msg0
  syscall

  b _etiq7

_etiq6:
  li  $v0,  4
  la  $a0,  _msg1
  syscall

  b _etiq3

_etiq2:
  li  $v0,  10
  syscall