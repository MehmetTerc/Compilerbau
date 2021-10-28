	.import	printi
	.import	printc
	.import	readi
	.import	readc
	.import	exit
	.import	time
	.import	clearAll
	.import	setPixel
	.import	drawLine
	.import	drawCircle
	.import	_indexError

	.code
	.align	4

	.export	summe
summe:
	sub	$29,$29,4		; allocate frame
	stw	$25,$29,0		; save old frame pointer
	add	$25,$29,4		; setup new frame pointer
	add	$8,$25,0
	ldw	$8,$8,0
	add	$9,$25,4
	ldw	$9,$9,0
	add	$8,$8,$9
	add	$9,$25,8
	ldw	$9,$9,0
	stw	$8,$9,0
	ldw	$25,$29,0		; restore old frame pointer
	add	$29,$29,4		; release frame
	jr	$31			; return

	.export	main
main:
	sub	$29,$29,32		; allocate frame
	stw	$25,$29,16		; save old frame pointer
	add	$25,$29,32		; setup new frame pointer
	stw	$31,$25,-20		; save return register
	add	$8,$25,-4
	stw	$8,$29,0		; store argument #0
	jal	readi
	add	$8,$25,-8
	stw	$8,$29,0		; store argument #0
	jal	readi
	add	$8,$25,-4
	ldw	$8,$8,0
	stw	$8,$29,0		; store argument #0
	add	$8,$25,-8
	ldw	$8,$8,0
	stw	$8,$29,4		; store argument #1
	add	$8,$25,-12
	stw	$8,$29,8		; store argument #2
	jal	summe
	add	$8,$25,-12
	ldw	$8,$8,0
	stw	$8,$29,0		; store argument #0
	jal	printi
	add	$8,$0,10
	stw	$8,$29,0		; store argument #0
	jal	printc
	ldw	$31,$25,-20		; restore return register
	ldw	$25,$29,16		; restore old frame pointer
	add	$29,$29,32		; release frame
	jr	$31			; return
