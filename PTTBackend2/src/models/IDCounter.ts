import mongoose from "mongoose";
const Schema = mongoose.Schema;

export const IDCounterSchema = new Schema({
    name: {
        type: String,
        default: "counters",
        unique: true
    },
    userId: {
        type: Number,
        default: 0,
        unique: true
    },
    projectId: {
        type: Number,
        default: 0,
        unique: true
    },
    sessionId: {
        type: Number,
        default: 0,
        unique: true
    }
}, {
    versionKey: false,
    strict: "throw"
});
