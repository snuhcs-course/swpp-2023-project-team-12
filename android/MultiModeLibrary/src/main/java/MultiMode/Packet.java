package MultiMode;

import java.io.Serializable;

public class Packet implements Serializable {
    private static final long serialVersionUID = 1L;

    // 데이터 유형을 나타내는 필드
    private int protocol;

    // 실제 데이터
    private MultiModeUser data;

    public Packet(int protocol, MultiModeUser data) {
        this.protocol = protocol;
        this.data = data;
    }

    public int getProtocol() {
        return protocol;
    }

    public MultiModeUser getData() {
        return data;
    }
}