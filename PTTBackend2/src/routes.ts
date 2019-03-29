import express from 'express';
import { UserController } from "./controllers/UserController";
import { ProjectController } from "./controllers/ProjectController";

export const routes = express.Router();

let userController = new UserController();
let projectController = new ProjectController();
const baseUrl = '/ptt';

routes.route(`${baseUrl}/`)
.get((req, res) => {res.send('Backend Server is working');});

routes.route(`${baseUrl}/users`)
.get((req, res) => {
    userController.getAllUsers()
    .then(obj => {
        res.status(obj["code"]).send(obj["result"]);
    })
    .catch(obj => {
        res.status(obj["code"]).send(obj["result"]);
    });
})
.post((req, res) => {
    userController.addUser(req.body)
    .then(obj => {
        res.status(obj["code"]).send(obj["result"]);
    })
    .catch(obj => {
        res.status(obj["code"]).send(obj["result"]);
    });
})

routes.route(`${baseUrl}/users/:userId`)
.get((req, res) => {
    userController.getUser(req.params["userId"], true)
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

routes.route(`${baseUrl}/users/:userId/projects`)
.get((req, res) => {
    projectController.getAllProjects()
    .then(obj => {
        res.status(obj["code"]).send(obj["result"]);
    })
    .catch(obj => {
        res.status(obj["code"]).send(obj["result"]);
    });
})
.post((req, res) => {
    projectController.addProject(req.params["userId"], req.body)
    .then(obj => {
        res.status(obj["code"]).send(obj["result"]);
    })
    .catch(obj => {
        res.status(obj["code"]).send(obj["result"]);
    });
})

routes.route(`${baseUrl}/users/:userId/projects/:projectId`)
.get((req, res) => {
    projectController.getProject(req.params["userId"], req.params["projectId"])
    .then(obj => {
        res.status(obj["code"]).send(obj["result"]);
    })
    .catch(obj => {
        res.status(obj["code"]).send(obj["result"]);
    });
})
.put((req, res) => {
    projectController.updateProject(req.params["userId"], req.params["projectId"], req.body)
    .then(obj => {
        res.status(obj["code"]).send(obj["result"]);
    })
    .catch(obj => {
        res.status(obj["code"]).send(obj["result"]);
    });
})
.delete((req, res) => {
    projectController.deleteProject(req.params["userId"], req.params["projectId"], true)
    .then(obj => {
        res.status(obj["code"]).send(obj["result"]);
    })
    .catch(obj => {
        res.status(obj["code"]).send(obj["result"]);
    });
})