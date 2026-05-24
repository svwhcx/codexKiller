package com.svwh.tools.axml.utils;

import android.content.Context;

import com.svwh.tools.constant.ReferenceResolver;

import java.io.IOException;

public class XmlChunk extends Chunk<XmlChunk.H> {

    private final ResourceMapChunk resourceMap = new ResourceMapChunk(this);
    StringPoolChunk stringPool = new StringPoolChunk(this);
    TagChunk content;

    public XmlChunk(Context context) {
        super(null);
        header = new H();
        this.context=context;
    }

    public static class H extends Chunk.Header {

        public H() {
            super(ChunkType.Xml);
        }

        public H(String test){
            super(ChunkType.Xml);
        }

        @Override
        public void writeEx(IntWriter w) throws IOException {

        }
    }

    @Override
    public void preWrite() {
        header.size=header.headerSize + content.calc()+stringPool.calc() + resourceMap.calc();
    }

    @Override
    public void writeEx(IntWriter w) throws IOException {
        stringPool.write(w);
        resourceMap.write(w);
        content.write(w);
    }

    @Override
    public XmlChunk root() {
        return this;
    }

    private ReferenceResolver referenceResolver;
    @Override
    public ReferenceResolver getReferenceResolver() {
        if (referenceResolver == null) referenceResolver= new DefaultReferenceResolver();
        return referenceResolver;
    }

}