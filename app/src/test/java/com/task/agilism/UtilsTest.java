package com.task.agilism;

import org.junit.Test;

import static org.junit.Assert.*;

public class UtilsTest {

    @Test
    public void isValidName() {
        String[] sameNames = {
                "Abundance",
                "Anxiety",
                "Bruxism",
                "Discipline",
                "Drug Addiction",
                "prashanth"
        };

        boolean[] output = new boolean[sameNames.length];

        boolean[] expected = new boolean[sameNames.length];

        for(int i=0; i<sameNames.length; i++){
            output[i] = Utils.isValidName(sameNames[i]);
            expected[i] = true;
        }

        assertArrayEquals(expected,output);

    }

    @Test
    public void isValidMailId() {

        String[] sampleEmailID = {
                "prashanth@hakunamatata.in",
                "prashanth@hakunamatata.in",
                "a123@gmail.com",
                "qwerty@yahoo.com",
                "Drug_Addiction@outlook.in",
                "prashanth@test.com"
        };

        boolean[] output = new boolean[sampleEmailID.length];

        boolean[] expected = new boolean[sampleEmailID.length];


        for(int i=0; i<sampleEmailID.length; i++){
            output[i] = Utils.isValidMailId(sampleEmailID[i]);
            expected[i] = true;
        }

        assertArrayEquals(expected,output);

    }
}