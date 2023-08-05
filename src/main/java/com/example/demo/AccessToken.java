/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.demo;

/**
 *
 * @author Shivam Gupta
 */import com.google.gson.annotations.SerializedName;

public class AccessToken {
    @SerializedName("access_token")
    private String accessToken;

    public String getAccessToken() {
        return accessToken;
    }
}
