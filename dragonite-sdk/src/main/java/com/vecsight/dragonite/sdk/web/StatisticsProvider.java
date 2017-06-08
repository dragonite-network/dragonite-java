package com.vecsight.dragonite.sdk.web;

import com.vecsight.dragonite.sdk.socket.DragoniteSocketStatistics;

import java.util.List;

public interface StatisticsProvider {

    public List<DragoniteSocketStatistics> getLatest();

}
