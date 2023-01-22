package hexlet.code.javaproject73.service;

import hexlet.code.javaproject73.dto.LabelDto;
import hexlet.code.javaproject73.model.Label;

public interface LabelService {

    Label createLabel(LabelDto labelDto);
    Label updateLabel(LabelDto labelDto, long id);
}
