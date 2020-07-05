        .data
_i:   .word 0
_o:   .word 0
_res:   .word 0
_msg0:  .asciiz "ingrese un numero"


        .text
        .globl main


main:
  li  $v0,  4
  la  $a0,  _msg0
  syscall

  li  $v0,  5
  syscall
  sw  $v0,  _o

  lw  $t0,  _o
  li  $t1, 3
  add $t2,  $t0, $t1

  sw  $t2,  _i

  lw  $t0,  _i
  li  $v0,  1
  move  $a0,  $t0
  syscall

li  $v0,  10
syscall