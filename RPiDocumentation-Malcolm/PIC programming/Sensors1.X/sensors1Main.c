/* 
 * File:   sensors1Main.c
 * Author: Malcolm
 *
 * Created on June 26, 2013, 8:19 AM
 */

// test the peripheral sensors of the PIC (thermometer, microphone, humidity sensor)

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


void configure_adc();

void adc_on();

void adc_off();

void choose_microphone();

void choose_thermometer();

void choose_hygrometer();

void read_sensor(char* msb, char* lsb);


int main(int argc, char** argv) {

    configure_adc();

    choose_microphone();

    _delay(10);

    adc_on();

    char highBits, lowBits;

    while (1) {
        read_sensor(&highBits, &lowBits);
    }

}


// adc procedure:
// 1. call configure_adc()
// 2. choose a device to read from
// 3. call adc_on()
// 4. take reading with read_sensor()
// 5. if switching devices, call adc_off() and goto 2.)
//
void configure_adc() {
    // configure ADC for reading froms AUDIO, TEMP, or HUMIDITY_LVL

    // set up FVR
    FVRCONbits.FVREN = 1;
    FVRCONbits.ADFVR = 0b10; // select the voltage
    while (FVRCONbits.FVRRDY != 1) {} // wait for the FVR to be setup

    // right justify converted data
    ADCON1bits.ADFM = 1;

    // set clock to frc
    ADCON1bits.ADCS = 0b111;

    // Vss is negative voltage reference
    ADCON1bits.ADNREF = 0;
    
    ADCON1bits.ADPREF = 0b00; // Vdd is positive voltage reference
    //ADCON1bits.ADPREF = 0b11; // FVR is positive voltage reference

    // set SOUND (AN7 aka RC3), TEMP (AN8 aka RC6), and HUMIDITY_LVL (AN9 aka RC7) to input
    TRISCbits.TRISC3 = 1;
    TRISCbits.TRISC6 = 1;
    TRISCbits.TRISC7 = 1;

    // set SOUND (AN7 aka RC3), TEMP (AN8 aka RC6), and HUMIDITY_LVL (AN9 aka RC7) to analog
    ANSELCbits.ANSC3 = 1;
    ANSELCbits.ANSC6 = 1;
    ANSELCbits.ANSC7 = 1;
}

void adc_on() {
    // enable ADC
    ADCON0bits.ADON = 1;
}

void adc_off() {
    // disable ADC
    ADCON0bits.ADON = 0;
}

void choose_microphone() {
    // set ADC to read from AN7 (b6-2 CHS 0b00111)
    ADCON0bits.CHS = 0b00111;

    // there must be a short delay between changing input and making a reading...
}

void choose_thermometer() {
    // set ADC to read from AN8
    ADCON0bits.CHS = 0b01000;

    // there must be a short delay between changing input and making a reading...
}

void choose_hygrometer() {
    // set ADC to read from AN9
    ADCON0bits.CHS = 0b01001;

    // there must be a short delay between changing input and making a reading...

    // True RH = (Sensor RH)/(1.0546-0.00216T), T in C
}

// this will set the arguments to the most significant and least
// significant bits of the result of adc
void read_sensor(char* msb, char* lsb) {
    // start the conversion
    ADCON0bits.ADGO = 1;

    // wait for the conversion to complete
    while(ADCON0bits.ADGO == 1) {}

    *msb = ADRESH;
    *lsb = ADRESL;
}