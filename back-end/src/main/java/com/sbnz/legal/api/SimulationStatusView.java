package com.sbnz.legal.api;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SimulationStatusView {
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Instant simulatedNow;
}
