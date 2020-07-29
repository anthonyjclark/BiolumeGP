/* 
 * File:   i2c1Main.c
 * Author: Malcolm
 *
 * Created on June 24, 2013, 12:42 PM
 */

// code copied and modified from main.c on http://www.pic-pi.com/

#include <stdio.h>
#include <stdlib.h>
#include <xc.h>

// set configuration bits
// CONFIG1
#pragma config FOSC = INTOSC    // Oscillator Selection (INTOSC oscillator: I/O function on CLKIN pin)
#pragma config WDTE = OFF       // Watchdog Timer Enable (WDT disabled)
#pragma config PWRTE = OFF      // Power-up Timer Enable (PWRT disabled)
#pragma config MCLRE = ON       // MCLR Pin Function Select (MCLR/VPP pin function is MCLR)
#pragma config CP = OFF         // Flash Program Memory Code Protection (Program memory code protection is disabled)
#pragma config CPD = OFF        // Data Memory Code Protection (Data memory code protection is disabled)
#pragma config BOREN = OFF      // Brown-out Reset Enable (Brown-out Reset disabled)
#pragma config CLKOUTEN = OFF   // Clock Out Enable (CLKOUT function is disabled. I/O or oscillator function on the CLKOUT pin)
#pragma config IESO = ON        // Internal/External Switchover (Internal/External Switchover mode is enabled)
#pragma config FCMEN = ON       // Fail-Safe Clock Monitor Enable (Fail-Safe Clock Monitor is enabled)

// CONFIG2
#pragma config WRT = OFF        // Flash Memory Self-Write Protection (Write protection off)
#pragma config PLLEN = ON       // PLL Enable (4x PLL enabled)
#pragma config STVREN = OFF     // Stack Overflow/Underflow Reset Enable (Stack Overflow or Underflow will not cause a Reset)
#pragma config BORV = LO        // Brown-out Reset Voltage Selection (Brown-out Reset Voltage (Vbor), low trip point selected.)
#pragma config LVP = OFF        // Low-Voltage Programming Enable (High-voltage on MCLR/VPP must be used for programming)


int debug = 1;

int main(int argc, char** argv) {

    // set the clock
    OSCCONbits.SCS = 0b10; // clock set by IRCF bits
    OSCCONbits.IRCF = 0b1111; // 16 MHz
    while (OSCSTATbits.HFIOFR == 0) {} // wait for clock to be ready

    // set some pins for debugging
    if (debug == 1) {
        // set RA0 and RA1 to digital output
        TRISAbits.TRISA0 = 0;
        TRISAbits.TRISA1 = 0;
        ANSELAbits.ANSA0 = 0;
        ANSELAbits.ANSA1 = 0;
        PORTAbits.RA0 = 0;
        PORTAbits.RA1 = 0;
    }

    // Initialize ic2

    // set i2c pins to digital input
    PORTBbits.RB4 = 1;
    PORTBbits.RB6 = 1;
    TRISBbits.TRISB4 = 1; // i2c SDA1 must be input
    TRISBbits.TRISB6 = 1; // i2c SCL1 must be input
    //ANSELBbits.ANSB4 = 0; // digital i2c
    //ANSELBbits.ANSB6 = 0; // digital i2c

    //SSP1CON1 = 0b00110110;
    SSP1CON1bits.SSPEN = 1; // enable SDA and SCL
    SSP1CON1bits.CKP = 1; // enable clock
    SSP1CON1bits.SSPM = 0b1110; //0b0110; // slave, 7 bit address

    //SSP1CON2 = 0b00000001;
    SSP1CON2bits.SEN = 1; // enable clock stretching

    // this register is set up differently in the two examples...
    //SSP1STAT = 0b11000000;
    SSP1STATbits.SMP = 1; // slew rate disabled (?)
    SSP1STATbits.CKE = 1; // compliant with smbus specification

    //SSP1CON3 = 0b00000011;
    SSP1CON3bits.AHEN = 1;
    SSP1CON3bits.DHEN = 1;

    SSPMSK = 0xff;
    SSPADD = 0x50; // address

    // enable interrupts
    INTCONbits.PEIE = 1; // peripheral interupts enabled (includes i2c)
    PIE1bits.SSP1IE = 1; // i2c interupt
    INTCONbits.GIE = 1; // general interrupt enable
    ei(); // enable interrupts

    _delay(10);

    while (1) {}

}

void interrupt isr(void) {

    if (PIR1bits.SSP1IF == 1) // i2c interupt
    {
        unsigned int data;
        unsigned int status;
        unsigned int address;

        // flip debug pins
        if (debug == 1) {
            // I believe these should be flipped even if communication with RPi is not successfull...
            PORTAbits.RA0 = 1;
            PORTAbits.RA1 = 1;

            debug = 0;
        }

        if (SSP1CON1bits.SSPOV == 1) {
            SSP1CON1bits.SSPOV = 0; //clear overflow
            data = SSP1BUF;
        }

        else {
            status = SSP1STAT & 0b00101101; // get DA, S, RW, and BF

            // find which state we are in

            // PIC's address received, master wants to write
            // S=1 && RW==0 && DA==0 && BF==1
            if ((status ^ 0b00001001) == 0) {
                address = SSP1BUF;
            }
                // data received
                // S=1 && RW==0 && DA==1 && BF==1
            else if ((status ^ 0b00101001) == 0) {
                data = SSP1BUF;
            }
                // PIC's address received, master wants to read
                // S=1 && RW==1 && DA==0 && BF==0
            else if ((status ^ 0b00001100) == 0) {
                address = SSP1BUF;
                SSP1CON1bits.WCOL = 0; // clear write collision flag
                SSP1BUF = 0xff; // data to send
            }
                // previous data byte sent, master wants to read
                // S=1 && RW==1 && DA==1 && BF==0
            else if ((status ^ 0b00101100) == 0) {
                SSP1CON1bits.WCOL = 0; // clear write collision flag
                SSP1BUF = 0xff; // data to send, low byte
            }
                // NACK received
                // S=1 && RW==0 && DA==1 && BF==0
            else if ((status ^ 0b00101000) == 0) {
                data = SSP1BUF;
            }
                // undefined, clear buffer
            else {
                data = SSP1BUF;
            }
        }

        SSP1CON1bits.CKP = 1; // release the clk
        PIR1bits.SSP1IF = 0; // clear interupt flag
    }
}
