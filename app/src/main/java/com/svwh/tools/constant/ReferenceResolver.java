package com.svwh.tools.constant;

import com.svwh.tools.axml.utils.ValueChunk;

public interface ReferenceResolver {

    int resolve(ValueChunk value, String ref);

}
