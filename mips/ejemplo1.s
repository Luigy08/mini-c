        .data
_x:   .word 0
_i:   .word 0
_letra:   .word 0
_letra2:   .word 0
_msg0:  .asciiz "Ingrese un numero: %d"


        .text
        .globl main


main:
  li  $t0,  4
  sw  $t0,  _i

  li  $t0,  4
  sw  $t0,  _letra

  li  $t0,  4
  li  $t1, 2
  mul $t2,  $t0, $t1

  lw  $t0,  _i
  add $t1,  $t0, $t2

  sw  $t1,  _x

  li  $v0,  4
  la  $a0,  _msg0
  syscall

  lw  $t0,  _x
  li  $v0,  1
  move  $a0,  $t0
  syscall

  li  $v0,  10
  syscall