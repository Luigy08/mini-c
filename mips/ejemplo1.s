        .data
_i:   .word 0
_o:   .word 0
_res:   .word 0
_msg0:  .asciiz "ingrese un numero: "


        .text
        .globl main


main:
  li  $v0,  4
  la  $a0,  _msg0
  syscall

  li  $v0,  5
  syscall
  sw  $v0,  _o

_etiq0:
  lw  $t0,  _o
  li  $t1,  0
  bgt  $t0, $t1,  _etiq1
  b _etiq2

_etiq1:
  lw  $t2,  _o
  li  $v0,  1
  move  $a0,  $t2
  syscall

  lw  $t3,  _o
  li  $t4, 1
  sub $t5,  $t3, $t4

  sw  $t5,  _o

  b _etiq0

_etiq2:
  li  $v0,  10
  syscall