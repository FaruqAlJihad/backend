package com.iconfest.agrosmart.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

/** Tujuh fitur yang dipakai model J48 (dataset Crop Recommendation). */
public record PredictRequest(
        @NotNull(message = "n wajib diisi") @DecimalMin("0") @DecimalMax("500") Double n,
        @NotNull(message = "p wajib diisi") @DecimalMin("0") @DecimalMax("500") Double p,
        @NotNull(message = "k wajib diisi") @DecimalMin("0") @DecimalMax("500") Double k,
        @NotNull(message = "temperature wajib diisi") @DecimalMin("-10") @DecimalMax("60") Double temperature,
        @NotNull(message = "humidity wajib diisi") @DecimalMin("0") @DecimalMax("100") Double humidity,
        @NotNull(message = "ph wajib diisi") @DecimalMin("0") @DecimalMax("14") Double ph,
        @NotNull(message = "rainfall wajib diisi") @DecimalMin("0") @DecimalMax("5000") Double rainfall
) {}
