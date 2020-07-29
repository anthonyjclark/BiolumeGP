/* 
 * File:   touch1Main.c
 * Author: Malcolm
 *
 * Created on June 18, 2013, 1:39 PM
 */

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

//# define _XTAL_FREQ 16000000    // 16 MHz

int debug = 1;

// for detecting touch
unsigned int oscillations;


int main(int argc, char** argv) {

    // set the clock
    OSCCONbits.SCS = 0b10; // clock set by IRCF bits

    //OSCCONbits.IRCF = 0b1111; // 16 MHz
    OSCCONbits.IRCF = 0b1011; // 1MHz
    while(OSCSTATbits.HFIOFR == 0) {}

    //OSCCONbits.IRCF = 0b0000; // 31 kHz
    //while(OSCSTATbits.LFIOFR == 0) {}

    // set some pins for debugging
    if (debug == 1)
    {
        // set RA0 and RA1 to digital output
        TRISAbits.TRISA0 = 0;
        TRISAbits.TRISA1 = 0;
        ANSELAbits.ANSA0 = 0;
        ANSELAbits.ANSA1 = 0;
        PORTAbits.RA0 = 0;
        PORTAbits.RA1 = 0;
    }


    // set the pins for touch sensing to analog input
    TRISCbits.TRISC0 = 1; // CPS4
    TRISCbits.TRISC1 = 1; // CPS5
    TRISCbits.TRISC2 = 1; // CPS6
    TRISBbits.TRISB4 = 1; // CPS10
    ANSELCbits.ANSC0 = 1;
    ANSELCbits.ANSC1 = 1;
    ANSELCbits.ANSC2 = 1;
    ANSELBbits.ANSB4 = 1;

    // set the capacitive sensing oscillator mode
    CPSCON0bits.CPSRNG = 0b11; // high current setting

    // set voltage references
    CPSCON0bits.CPSRM = 0; // use fixed voltage refs for now

    // configure timer1 to count from touch oscillator
    T1CONbits.TMR1CS = 0b11; // set the timer to count from CS module
    T1CONbits.nT1SYNC = 1; // disable synch with system clock

    // set the timer1 gate to toggle mode with timer0 overflow as the gate control
    //T1GCONbits.TMR1GE = 1; // turn on gate
    //T1GCONbits.T1GTM = 1; // turn on toggle mode
    //T1GCONbits.T1GSS = 0b01; // set gate source to timer0 overflow
    //T1GCONbits.T1GPOL = 1; // count when T1G is 1

    // initialize timer0 for fixed time base
    INTCONbits.TMR0IE = 1; // enable timer0 interrupt
    INTCONbits.TMR0IF = 0; // clear timer0 interrupt flag    
    OPTION_REGbits.PSA = 0; // turn prescalar on/off
    OPTION_REGbits.PS = 0b111; // set prescalar
    OPTION_REGbits.TMR0CS = 0; // turn on timer0

    // enable interrupts
    INTCONbits.GIE = 1;
    ei();

    _delay(10);

    // restart the timers and select a CS pin
    CPSCON0bits.CPSON = 1; // turn on cap. sensor
    CPSCON1bits.CPSCH = 0b0110; // read from CPS6
    TMR0 = 0x00; // restart timer0
    TMR1H = 0x00; // restart timer1
    TMR1L = 0x00;
    T1CONbits.TMR1ON = 1; // start timer1


    while (1) {

        //_delay(1000000);
        //PORTA ^= 0b00000011;

        //if (INTCONbits.TMR0IF == 1) {
        //    INTCONbits.TMR0IF = 0;
        //    PORTA ^= 0b00000011;
        //}

    }

}

void interrupt isr(void) {

    if (INTCONbits.TMR0IE && INTCONbits.TMR0IF) {

         T1CONbits.TMR1ON = 0; // turn off timer1

        // count the number of oscillations from timer1
        oscillations = TMR1L + (unsigned int)(TMR1H << 8);

        // compare to threshold to detect touch
        // threshold for 1MHz fosc, 1:256 prescalar is 0x845E
        if (oscillations > 0x80b5) { // 0x845e
            PORTAbits.RA0 = 0;
            PORTAbits.RA1 = 0;
        }
        else {
            PORTAbits.RA0 = 1;
            PORTAbits.RA1 = 1;
        }

        INTCONbits.TMR0IF = 0; // clear interrupt flag

        // reset timer1 and timer0
       
        TMR0 = 0x00; // restart timer0
        TMR1H = 0x00; // restart timer1
        TMR1L = 0x00;
        T1CONbits.TMR1ON = 1; // start timer1


    }

    // handle other interrupts...


    // if timer0 has overflowed while other interrupts are serviced, measurements are no good
    /***
    if (INTCONbits.TMR0IE && INTCONbits.TMR0IF) {

        INTCONbits.TMR0IF = 0; // clear the interrupt flag

        // reset timer1 and timer0
        T1CONbits.TMR1ON = 0; // turn off timer1
        TMR0 = 0x00; // restart timer0
        TMR1H = 0x00; // restart timer1
        TMR1L = 0x00;
        T1CONbits.TMR1ON = 1; // start timer1
    }
    ***/
}

