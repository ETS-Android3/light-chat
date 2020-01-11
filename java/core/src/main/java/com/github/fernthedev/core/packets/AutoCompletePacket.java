package com.github.fernthedev.core.packets;

import com.github.fernthedev.core.data.LightCandidate;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@PacketInfo(name = "AUTO_COMPLETE_PACKET")
public class AutoCompletePacket extends Packet {

    private List<LightCandidate> candidateList = new ArrayList<>();

    @NonNull
    private List<String> words;

    public AutoCompletePacket(List<String> words) {
        this.words = words;
    }
}
