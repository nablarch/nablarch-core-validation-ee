package com.example;

import java.util.List;
import jakarta.validation.constraints.NotNull;

import nablarch.core.validation.ee.Length;
import nablarch.core.validation.ee.NumberRange;
import nablarch.core.validation.ee.Size;
import nablarch.core.validation.ee.SystemChar;

/**
 * TODO write document comment.
 *
 * @author T.Kawasaki
 */
public class SampleDomain {

    @Length(max = 5)
    @SystemChar(charsetDef = "全角カタカナ")
    public String kanaName;

    @NumberRange(min = 0, max = 100)
    public Integer score;


}
