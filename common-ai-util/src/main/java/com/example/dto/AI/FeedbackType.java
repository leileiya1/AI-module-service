package com.example.dto.AI;

/**
 * 用户反馈类型的枚举。
 * 使用枚举可以防止传入无效的反馈类型字符串，增强代码的健壮性。
 */
public enum FeedbackType {
    LIKED,      // 满意，直接采纳
    DISLIKED,   // 不满意，换一条或不使用
    EDITED      // 编辑后采纳
}