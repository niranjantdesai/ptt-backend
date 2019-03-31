import mongoose from "mongoose";
const Schema = mongoose.Schema;

export const SessionSchema = new Schema({
    id: { 
        type: Number,
        required: 'ID is required'
    },
    startTime: {
        type: String,
        required: 'Start time is required'
    },
    endTime: {
        type: String,
        required: 'End time is required'
    },
    counter: { 
        type: Number,
        default: 0
    }
}, {
    versionKey: false,
    strict: "throw"
});