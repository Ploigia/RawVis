package gr.ploigia.rawvis.util;

import com.beust.jcommander.IStringConverter;
import com.google.common.collect.Range;


public class RangeConverter implements IStringConverter<Range<Float>> {

    @Override
    public Range<Float> convert(String s) {
        return Utils.convertToRange(s);
    }
}
