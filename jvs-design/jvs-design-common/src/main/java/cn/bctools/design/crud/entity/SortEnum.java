package cn.bctools.design.crud.entity;

import lombok.Getter;

@Getter
public enum SortEnum {
    desc(-1), asc(1);

    public int sort;

    SortEnum(int sort) {
        this.sort = sort;
    }
}
