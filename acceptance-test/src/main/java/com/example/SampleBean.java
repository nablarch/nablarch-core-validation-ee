package com.example;

import java.util.List;
import jakarta.ws.rs.QueryParam;

import nablarch.core.validation.ee.Domain;
import nablarch.core.validation.ee.Required;
import nablarch.core.validation.ee.Size;

/**
 * TODO write document comment.
 *
 * @author T.Kawasaki
 */
public class SampleBean {

    @QueryParam("kanaName")
    @Required
    @Domain("kanaName")
    public String kanaName;

    @QueryParam("score")
    @Domain("score")
    public Integer score;

    @QueryParam("list")
    @Size(max = 2)
    public List<String> list;

    @Override
    public String toString() {
        return "SampleBean{" +
                "kanaName='" + kanaName + '\'' +
                ", score=" + score +
                ", list=" + list +
                '}';
    }
}
