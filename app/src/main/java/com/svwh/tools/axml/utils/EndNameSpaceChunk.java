package com.svwh.tools.axml.utils;

import java.io.IOException;

public class EndNameSpaceChunk extends Chunk<EndNameSpaceChunk.H> {

    private final StartNameSpaceChunk start;

    public class H extends Chunk.NodeHeader {
        public H() {
            super(ChunkType.XmlEndNamespace);
            size = 0x18;
        }
    }

    public EndNameSpaceChunk(Chunk parent, StartNameSpaceChunk start) {
        super(parent);
        header = new H();
        this.start = start;
    }

    @Override
    public void writeEx(IntWriter w) throws IOException {
        start.writeEx(w);
    }

}