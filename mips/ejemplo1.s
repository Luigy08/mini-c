        .data
_i:   .word 0
_o:   .word 0
_res:   .word 0


        .text
        .globl main


main:
  li  $t0,  0
  sw  $t0,  _i

_etiq0:
  lw  $t0,  _i
  li  $t1,  10
  blt  $t0, $t1,  _etiq1
  b _etiq2

_etiq3:
  lw  $t0,  _i
  li  $t1, 1
  add $t2,  $t0, $t1

  sw  $t2,  _i

  b _etiq0

_etiq1:
  lw  $t0,  _i
  li  $v0,  1
  move  $a0,  $t0
  syscall

  b _etiq3

_etiq2:
  li  $v0,  10
  syscall