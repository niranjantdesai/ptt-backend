// ptt in the uri
// id is supposed to be an integer, not a string

import express from 'express';
import { UserController } from "./controllers/UserController";

export const routes = express.Router();

let userController = new UserController();

routes.route('/')
.get((req, res) => {res.send('Backend Server is working');});

routes.route('/users')
.post((req, res) => {
    userController.addUser(req.body)
    .then(obj => {
        res.status(obj["code"]).send(obj["result"]);
    })
    .catch(obj => {
        res.status(obj["code"]).send(obj["result"]);
    });
})

routes.route('/users/:userId')
.get((req, res) => {
    userController.getUser(req.params["userId"])
    .then(obj => {
        res.status(obj["code"]).send(obj["result"]);
    })
    .catch(obj => {
        res.status(obj["code"]).send(obj["result"]);
    });
})
.put((req, res) => {
    userController.updateUser(req.params["userId"], req.body)
    .then(obj => {
        res.status(obj["code"]).send(obj["result"]);
    })
    .catch(obj => {
        res.status(obj["code"]).send(obj["result"]);
    });
})
.delete((req, res) => {
    userController.deleteUser(req.params["userId"])
    .then(obj => {
        res.status(obj["code"]).send(obj["result"]);
    })
    .catch(obj => {
        res.status(obj["code"]).send(obj["result"]);
    })
})