package com.sbnz.legal.domain.enums;

/**
 * Procedural steps / goals in the lifecycle of a legal matter. Steps form an acyclic
 * prerequisite graph (a step may require other steps and/or documents). The recursive
 * backward-chaining queries derive whether a top-level goal can be reached.
 */
public enum ProcedureStep {
    // Debt collection (naplata potraživanja)
    ESTABLISH_CLAIM_BASIS,
    SEND_PRELAWSUIT_NOTICE,
    FILE_LAWSUIT,

    // Damages (naknada štete)
    ESTABLISH_DAMAGE_BASIS,
    FILE_DAMAGES_CLAIM,

    // Property (imovinsko-pravni)
    ESTABLISH_OWNERSHIP,
    FILE_PROPERTY_CLAIM
}
