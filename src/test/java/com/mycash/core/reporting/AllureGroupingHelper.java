package com.mycash.core.reporting;

import io.qameta.allure.Allure;
import io.qameta.allure.model.StepResult;

import java.util.UUID;

public final class AllureGroupingHelper {

    private AllureGroupingHelper() {
    }

    /**
     * Start Workbook
     */
    public static String startWorkbook(String workbook) {

        String uuid = UUID.randomUUID().toString();

        Allure.getLifecycle().startStep(
                uuid,
                new StepResult()
                        .setName("📘 Workbook : " + workbook)
        );

        return uuid;
    }

    /**
     * Finish Workbook
     */
    public static void stopWorkbook(String uuid) {

        Allure.getLifecycle().stopStep(uuid);

    }

    /**
     * Start Sheet
     */
    public static String startSheet(String sheet) {

        String uuid = UUID.randomUUID().toString();

        Allure.getLifecycle().startStep(
                uuid,
                new StepResult()
                        .setName("📂 Sheet : " + sheet)
        );

        return uuid;
    }

    /**
     * Finish Sheet
     */
    public static void stopSheet(String uuid) {

        Allure.getLifecycle().stopStep(uuid);

    }

}